package com.walkingtweet.kichi.walkingtweet;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Date;
import java.util.List;

public class AccelerometerAdapter implements SensorEventListener {

    private SensorManager manager;
    long changeTime = 0;
    double vectorSize = 0;

    // �d���J�E���g�h�~�p�t���O
    boolean counted = true;

    // �����J�E���^�[
    long step_counter = 0;

    // �P�O�̃x�N�g����
    double oldVectorSize = 0;

    // 臒l
    double threshold = 15;
    // �������]���̍ŏ�臒l
    double thresholdMin = 1;
    // �x�N�g���ω����o���Ȃ����Ԃ�臒l
    long thresholdTime = 190;

    // X����������
    boolean vecx = true;
    // Y����������
    boolean vecy = true;
    // Z����������
    boolean vecz = true;
    // �����x�����̓]����
    int vecchangecount = 0;


    private float oldx = 0;
    private float oldy = 0;
    private float oldz = 0;

    private float dx = 0;
    private float dy = 0;
    private float dz = 0;

    public long getStepCounter() {
        return step_counter;
    }

    public float getDx() {
        return dx;
    }

    public void setDx(float dx) {
        this.dx = dx;
    }

    public float getDy() {
        return dy;
    }

    public void setDy(float dy) {
        this.dy = dy;
    }

    public float getDz() {
        return dz;
    }

    public void setDz(float dz) {
        this.dz = dz;
    }

    public AccelerometerAdapter(SensorManager manager) {
        List<Sensor> sensors = manager
                .getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensors.size() > 0) {
            Sensor s = sensors.get(0);
            manager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stopSensor() {
        if ( manager != null )
            manager.unregisterListener(this);
        manager = null;
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            dx = event.values[0] - oldx;
            dy = event.values[1] - oldy;
            dz = event.values[2] - oldz;

            // �x�N�g���ʂ��s�^�S���X�̒�`���狁�߂�B
            // �����m�Ȓl�͕K�v�łȂ��A����d�͂��畽�����܂ŋ��߂�K�v�͂Ȃ�
            // vectorSize = Math.sqrt((double)(dx*dx+dy*dy+dz*dz));
            vectorSize = (double) (dx * dx + dy * dy + dz * dz);
            // �x�N�g���v�Z�������ɍs���ƌv�Z�ʂ��オ�邽�߁A�ȈՓI�ȕ��������߂�B
            // ���ʂ̃x�N�g���ʂ���������̔��]���������ꍇ�i�����������ꍇ�j
            // vecchangecount��SENSOR_DELAY_NORMAL�̏ꍇ�A200ms���x���
            // �����x�ω������o�ł��Ȃ����߂̐�p�����B���x���グ��ƕs�v
            // ����ɐ��x����邢���Ƃ���A�A���̃x�N�g���ω��͌��m���Ȃ��B
            long dt = new Date().getTime() - changeTime;
            boolean dxx = Math.abs(dx) > thresholdMin && vecx != (dx >= 0);
            boolean dxy = Math.abs(dy) > thresholdMin && vecy != (dy >= 0);
            boolean dxz = Math.abs(dz) > thresholdMin && vecz != (dz >= 0);
            if (vectorSize > threshold && dt > thresholdTime
                    && (dxx || dxy || dxz)) {
                vecchangecount++;
                changeTime = new Date().getTime();
            }
            // �x�N�g���ʂ������ԂŌ������Q��i�㉺�^���Ƃ݂Ȃ��j�ς�����ꍇ
            // �܂��́A�x�N�g���ʂ����l����������i�Î~�Ƃ݂Ȃ��j�ꍇ�A�J�E���g����
            if (vecchangecount > 1 || vectorSize < 1) {
                counted = false;
                vecchangecount = 0;
            }
            // �J�E���g���ŁA臒l�𒴂���x�N�g���ʂ�����ꍇ�A�J�E���g
            if (!counted && vectorSize > threshold) {
                step_counter++;
                counted = true;
                vecchangecount = 0;
            }
            // �J�E���g���̉����x�̌�����ۑ�
            vecx = dx >= 0;
            vecy = dy >= 0;
            vecz = dz >= 0;
            // ��ԍX�V
            oldVectorSize = vectorSize;

            oldx = event.values[0];
            oldy = event.values[1];
            oldz = event.values[2];
        }
    }

}
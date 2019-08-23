package ycble.runchinaup.core;

public enum SystemBluetoothState {
            /**
             * 断开了
             */
            StateDisConn,
            /**
             * 手机系统蓝牙打开
             */
            StateOnBle,
            /**
             * 手机系统蓝牙关闭
             */
            StateOffBle,
            /**
             * 正在打开手机系统蓝牙
             */
            StateOpeningBle,
            /**
             * 正在关闭手机系统蓝牙
             */
            StateClosingBle;
        }
/*
 * Copyright (c) 2018. by RFControls. All Rights Reserved.
 * www.http://rfcontrols.com/
 * Design and Programming by Alex Dovby
 */

package com.jsc.smarthome.html;

public class JSConstants {

    public static final String INTERFACE_NAME = "NativeApplication";
    // ---------------------------------------------
    public static final String REQUEST = "request";
    public static final String RESPONSE = "response";
    // ---------------------------------------------

    // events from client --------
    public static final int EVT_MAIN_TEST = 0;
    public static final int EVT_READY = 1;
    public static final int EVT_BACK = 4;
    public static final int EVT_SYNC = 5;
    // ---------------------------

    // command for client --------
    public static final int CMD_INIT = 1000;
    // command cur app -----------
    public static final int CMD_MEASUREMENT_RESULT = 5002;
    public static final int CMD_SHOW_LIST = 5003;
    public static final int EVT_EXIT = 35;

    public static final int EVT_EXO = 777;
    public static final int EVT_EXO_RESPONSE = 888;
    // ---------------------------

    // events  app ---------------
    public static final int EVT_PAGE_FINISHED = 889;
    // ---------------------------
}

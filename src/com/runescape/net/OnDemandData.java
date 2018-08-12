package com.runescape.net;
// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import com.runescape.link.NodeSub;

public final class OnDemandData extends NodeSub {

    public OnDemandData()
    {
        incomplete = true;
    }

    public int dataType;
    public byte buffer[];
    public int ID;
    public boolean incomplete;
    public int loopCycle;
}

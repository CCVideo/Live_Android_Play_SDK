package com.bokecc.livemodule.bean;

public class MarqueeAction {
    private int index;
    private int duration;
    private float startXpos;
    private float startYpos;
    private float startAlpha;

    private float endXpos;
    private float endYpos;
    private float endAlpha;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public float getStartXpos() {
        return startXpos;
    }

    public void setStartXpos(float startXpos) {
        this.startXpos = startXpos;
    }

    public float getStartYpos() {
        return startYpos;
    }

    public void setStartYpos(float startYpos) {
        this.startYpos = startYpos;
    }

    public float getStartAlpha() {
        return startAlpha;
    }

    public void setStartAlpha(float startAlpha) {
        this.startAlpha = startAlpha;
    }

    public float getEndXpos() {
        return endXpos;
    }

    public void setEndXpos(float endXpos) {
        this.endXpos = endXpos;
    }

    public float getEndYpos() {
        return endYpos;
    }

    public void setEndYpos(float endYpos) {
        this.endYpos = endYpos;
    }

    public float getEndAlpha() {
        return endAlpha;
    }

    public void setEndAlpha(float endAlpha) {
        this.endAlpha = endAlpha;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}

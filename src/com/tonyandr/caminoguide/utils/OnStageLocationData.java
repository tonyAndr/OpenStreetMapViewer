package com.tonyandr.caminoguide.utils;

/**
 * Created by Tony on 24-Feb-15.
 */
public class OnStageLocationData {

    public int stageId;
    public int pointId;
    public int partId;
    public boolean alt;
    public double localMin;

    public OnStageLocationData(int stageId, int pointId) {
        this.stageId = stageId;
        this.pointId = pointId;
    }


    public OnStageLocationData(int stageId, int partId, int pointId, boolean alt) {
        this.stageId = stageId;
        this.pointId = pointId;
        this.partId = partId;
        this.alt = alt;
    }

    public OnStageLocationData(double localMin, int partId, int pointId, boolean alt) {
        this.localMin = localMin;
        this.pointId = pointId;
        this.partId = partId;
        this.alt = alt;
    }

    public OnStageLocationData(double localMin, int pointId) {
        this.localMin = localMin;
        this.pointId = pointId;
    }
}

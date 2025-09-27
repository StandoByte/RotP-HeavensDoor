package com.yaricktt.heavensdoor.client.render;

import com.github.standobyte.jojo.client.render.entity.model.stand.HumanoidStandModel;
import com.yaricktt.heavensdoor.entity.HeavensDoorEntity;


public class HeavensDoorModel extends HumanoidStandModel<HeavensDoorEntity> {
    public HeavensDoorModel() {
        super();
    }

    @Override
    public void prepareMobModel(HeavensDoorEntity entity, float walkAnimPos, float walkAnimSpeed, float partialTick) {
        super.prepareMobModel(entity, walkAnimPos, walkAnimSpeed, partialTick);
    }
}
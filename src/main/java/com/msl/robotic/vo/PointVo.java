package com.msl.robotic.vo;

import com.msl.common.result.Result;
import lombok.Data;

@Data
public class PointVo {
    Integer id;

    String name;

    Object[] point;

    public static PointVo of(Integer id, Object[] point) {
        PointVo pointVo = new PointVo();
        pointVo.setId(id);
        pointVo.setPoint(point);
        return pointVo;
    }

}

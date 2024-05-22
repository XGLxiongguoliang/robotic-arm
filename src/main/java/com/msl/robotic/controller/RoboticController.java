package com.msl.robotic.controller;

import com.alibaba.fastjson.JSONObject;
import com.msl.common.result.Result;
import com.msl.robotic.param.PointParam;
import com.msl.robotic.service.RoboticService;
import com.msl.robotic.vo.PointVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/robotic")
public class RoboticController {

    @Autowired
    private RoboticService roboticService;

    @ApiOperation(value = "建立连接", notes = "<br>by kzl")
    @GetMapping("/connectET")
    public Result<Integer> connectET() {
        return Result.ok(roboticService.connectET());
    }

    @ApiOperation(value = "断开连接", notes = "<br>by kzl")
    @GetMapping("/disconnectET")
    public Result<Void> disconnectET() {
        roboticService.disconnectET();
        return Result.ok();
    }

    @ApiOperation(value = "获得当前节点位姿", notes = "<br>by kzl")
    @GetMapping("/getNowPoint")
    public Result<PointVo> getNowPoint() {
        return Result.ok(roboticService.getNowPoint());
    }

    @ApiOperation(value = "根据坐标移动", notes = "<br>by kzl")
    @PostMapping("/movePoint")
    public Result<Integer> movePoint(@RequestBody PointParam param) throws InterruptedException {
        return Result.ok(roboticService.movePoint(param));
    }

//    @ApiOperation(value = "获得某节点位姿", notes = "<br>by kzl")
//    @GetMapping("/getPoint")
//    public Result<PointVo> getPoint(@RequestParam(value = "id") @NotBlank(message = "id不可为空") Integer id){
//        return Result.ok(roboticService.getPoint(id));
//    }

    @ApiOperation(value = "获得某节点坐标", notes = "<br>by kzl")
    @GetMapping("/getWeizi")
    public Result<PointVo> getWeizi(@RequestParam(value = "id") @NotBlank(message = "id不可为空") Integer id){
        return Result.ok(roboticService.getWeizi(id));
    }

    @ApiOperation(value = "下一步", notes = "<br>by kzl")
    @PostMapping("/next")
    public Result<PointVo> next(@RequestBody PointParam param) throws InterruptedException {
        return Result.ok(roboticService.next(param));
    }
    @ApiOperation(value = "所有坐标节点", notes = "<br>by kzl")
    @GetMapping("/listPoints")
    public Result<List<PointVo>> listPoints()  {
        return Result.ok(roboticService.listPoints());
    }

    @ApiOperation(value = "根据坐标旋转", notes = "<br>by kzl")
    @PostMapping("/moveWeizi")
    public Result<Integer> moveWeizi(@RequestBody PointParam param) throws InterruptedException {
        return Result.ok(roboticService.moveWeizi(param));
    }

    @ApiOperation(value = "轨迹运动", notes = "<br>by kzl")
    @GetMapping("/movePath")
    public Result<Integer> movePath(@RequestParam(value = "list") List<Integer> list) {
        return Result.ok(roboticService.movePath(list));
    }

    @ApiOperation(value = "保存节点", notes = "<br>by kzl")
    @PostMapping("/savePoint")
    public Result<Integer> savePoint(@RequestBody PointParam param) throws InterruptedException {
        return Result.ok(roboticService.savePoint(param));
    }

    @ApiOperation(value = "删除节点", notes = "<br>by kzl")
    @GetMapping("/deletePoint")
    public Result<Integer> deletePoint(@RequestParam(value = "id") @NotBlank(message = "id不可为空") Integer id) throws InterruptedException {
        return Result.ok(roboticService.deletePoint(id));
    }

    @ApiOperation(value = "get请求测试", notes = "<br>by kzl")
    @GetMapping("/testGet")
    public Result<Integer> testGet() {
        return Result.ok(1);
    }

    @ApiOperation(value = "post请求测试", notes = "<br>by kzl")
    @PostMapping("/testPost")
    public Result<String> testPost(@RequestBody PointParam param) {
        return Result.ok(new JSONObject().toJSONString());
    }

    @ApiOperation(value = "机械臂连接爪子", notes = "<br>by kzl")
    @GetMapping("/ELITEconnectGRIPPER")
    public Result<Integer> ELITEconnectGRIPPER() {
        return Result.ok(roboticService.ELITEconnectGRIPPER());
    }
}

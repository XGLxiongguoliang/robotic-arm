import com.alibaba.fastjson.JSONObject;
import com.msl.robotic.util.EdictUtil;

import java.net.Socket;

import static com.msl.robotic.util.EdictUtil.connectETController;

public class Test {
    private static Socket socket = null;
    public static void main(String[] args) {
        socket = connectETController("192.168.1.200", 8055);
        JSONObject robotState = EdictUtil.getRobotState(socket);
        System.out.println(JSONObject.toJSONString(robotState));
    }
}

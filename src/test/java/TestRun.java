import com.hz.perfma.request.SimpleRequest;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

/**
 * @Description
 * @Author liusu <xieqiyong66@gmail.com>
 * @Version
 * @Date 2021/8/24
 */
public class TestRun {

    public static void main(String[] args) {
        Arguments params = new Arguments();
        params.addArgument("host", "gateway.loveyunfamily.cn");
        params.addArgument("uri", "/api/articles?page=1");
        params.addArgument("method", "POST");
        params.addArgument("requestHeader", "");
        params.addArgument("requestBody", "");
        params.addArgument("protocol", "http://");
        params.addArgument("isForm", "");
        // 实例化请求，并执行请求
        JavaSamplerContext context = new JavaSamplerContext(params);
        SimpleRequest sttest = new SimpleRequest();
        sttest.setupTest(context);
        for(int i=0;i<20;i++){
            sttest.runTest(context);
        }
        sttest.teardownTest(context);
    }
}

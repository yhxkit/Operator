package com.sample.operator.app.common.mnt;

import com.sample.operator.app.common.connection.HyperTextTransferProtocol;
import com.sample.operator.app.common.util.JsonUtil;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class MntBiz
{
    private final MeterRegistry meterRegistry;

    private final HyperTextTransferProtocol http;
    private final JsonUtil jsonUtil;

    public Map<String, Object> getMetrics()
    {
        Map<String, Object> allMetrics = new HashMap<>();

        // 등록된 모든 Meter 객체를 순회
        meterRegistry.getMeters().forEach(meter ->
        {
            // Meter의 이름과 유형을 가져와서 로그 출력 또는 데이터 처리
            String name = meter.getId().getName();
            String type = meter.getId().getType().toString();
            System.out.println("Meter: " + name + ", Type: " + type);

            // Meter 유형에 따라 값을 추출하여 Map에 저장
            // 각 Meter 유형은 여러 값을 가질 수 있지만, 여기서는 간단한 예시로 Gauge 값만 처리
            meter.measure().forEach(measurement -> {
                String tagName = measurement.getStatistic().getTagValueRepresentation();
                double value = measurement.getValue();
                allMetrics.put(name + "." + tagName, value);
            });
        });


        System.out.println(allMetrics);

        return allMetrics;
    }



    public void recordRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();

        // Counter 이름 및 태그로 URL, IP 추가
        Counter counter = meterRegistry.counter("http_requests_total", "url", uri, "ip", ip);
        counter.increment();
    }
}

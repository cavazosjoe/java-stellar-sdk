package org.stellar.sdk.requests;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class InterceptorTest {
    @Test
    public void testClientIdentificationInterceptor() throws IOException, InterruptedException {
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.start();
        mockWebServer.enqueue(new MockResponse());

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .addInterceptor(new ClientIdentificationInterceptor()).build();
        okHttpClient.newCall(new Request.Builder().url(mockWebServer.url("/")).build()).execute();

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("java-stellar-sdk", request.getHeader("X-Client-Name"));
        assertEquals("dev", request.getHeader("X-Client-Version"));

        mockWebServer.shutdown();
    }
}

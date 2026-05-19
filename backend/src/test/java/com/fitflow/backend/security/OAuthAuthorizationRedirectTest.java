package com.fitflow.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.profiles.active=oauth",
        "GOOGLE_CLIENT_ID=test-google-client-id",
        "GOOGLE_CLIENT_SECRET=test-google-client-secret",
        "KAKAO_CLIENT_ID=test-kakao-client-id",
        "KAKAO_CLIENT_SECRET=test-kakao-client-secret",
        "NAVER_CLIENT_ID=test-naver-client-id",
        "NAVER_CLIENT_SECRET=test-naver-client-secret"
})
@AutoConfigureMockMvc
class OAuthAuthorizationRedirectTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void googleAuthorizationEndpointRedirectsToProvider() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", startsWith("https://accounts.google.com/o/oauth2/v2/auth")));
    }

    @Test
    void kakaoAuthorizationEndpointRedirectsToProvider() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/kakao"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", startsWith("https://kauth.kakao.com/oauth/authorize")));
    }

    @Test
    void naverAuthorizationEndpointRedirectsToProvider() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/naver"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", startsWith("https://nid.naver.com/oauth2.0/authorize")));
    }
}

package com.malex.restful.get;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.malex.restful.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/*
 * Use GET requests to retrieve resource representation/information only – and not modify it in any way.
 *
 * Save:
 * An HTTP method is safe if it doesn't alter the state of the server.
 * In other words, a method is safe if it leads to a read-only operation.
 * Several common HTTP methods are safe: GET, HEAD, or OPTIONS. All safe methods are also idempotent,
 * but not all idempotent methods are safe. For example, PUT and DELETE are both idempotent but unsafe.
 *
 * Idempotency:
 * As GET requests do not change the resource’s state, these are said to be safe methods.
 * Additionally, GET APIs should be idempotent. Making multiple identical requests must produce the same result
 * every time until another API (POST or PUT) has changed the state of the resource on the server.
 *
 * GET API Response Codes:
 *
 * 200 (OK): For any given HTTP GET API, if the resource is found on the server, then it must return HTTP response
 * code 200 (OK) along with the response body, which is usually either XML or JSON content.
 *
 * 404 (NOT FOUND): In case the resource is NOT found on the server then API must
 * return HTTP response code 404 (NOT FOUND).
 *
 * 400 (BAD REQUEST): Similarly, if it is determined that the GET request itself is not correctly formed then
 * the server will return the HTTP response code 400 (BAD REQUEST).
 */
@WebMvcTest
class HTTP_GET_400_BadRequest {

  @Autowired private MockMvc mvc;

  @MockitoBean(name = "userRepository")
  private UserRepository repository;

  /*
   * Case 1: invalid request - id parameter
   *
   * If the client sent a garbled request (invalid param, invalid URI format, etc), then they should get back a 400.
   *
   * link: https://medium.com/nerd-for-tech/navigating-http-status-codes-for-rest-apis-39f25fcd8cc6
   */
  @Test
  void http400_bad_request() throws Exception {
    mvc.perform(get("/api/v1/users/-1")).andExpect(status().isBadRequest());
  }
}

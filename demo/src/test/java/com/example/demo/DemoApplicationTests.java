package com.example.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.demo.repo.ProductoRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class DemoApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ProductoRepo productoRepo;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void cleanUp() {
		productoRepo.deleteAll();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void flujoCompletoCrudProducto() throws Exception {
		String crearPayload = """
				{
				  "nombre": "Creatina",
				  "descripcion": "Creatina de 500 g HoliXlab",
				  "lote": "12345",
				  "precioUnitario": 500.5,
				  "fechaCaducidad": "2026-05-04"
				}
				""";

		// POST /producto
		MvcResult postResult = mockMvc.perform(post("/producto")
				.contentType(MediaType.APPLICATION_JSON)
				.content(crearPayload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.nombre").value("Creatina"))
				.andReturn();

		JsonNode created = objectMapper.readTree(postResult.getResponse().getContentAsString());
		long id = created.get("id").asLong();

		// GET /producto/{id}
		mockMvc.perform(get("/producto/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.nombre").value("Creatina"))
				.andExpect(jsonPath("$.lote").value("12345"));

		// GET /productos
		mockMvc.perform(get("/productos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));

		// PUT /producto/{id}
		String actualizarPayload = """
				{
				  "nombre": "Proteína de fresa",
				  "descripcion": "Proteína de suero de leche 25 g, 2.2 kg",
				  "lote": "12345",
				  "precioUnitario": 500.5,
				  "fechaCaducidad": "2027-06-07"
				}
				""";

		mockMvc.perform(put("/producto/{id}", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(actualizarPayload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.nombre").value("Proteína de fresa"));

		// Confirmar actualización con GET /producto/{id}
		mockMvc.perform(get("/producto/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Proteína de fresa"))
				.andExpect(jsonPath("$.fechaCaducidad").value("2027-06-07"));

		// DELETE /producto/{id}
		mockMvc.perform(delete("/producto/{id}", id))
				.andExpect(status().isOk())
				.andExpect(content().json("{\"success\":true}"));

		// Confirmar eliminación con GET /productos
		mockMvc.perform(get("/productos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}
}

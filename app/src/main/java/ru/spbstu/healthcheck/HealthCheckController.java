package ru.spbstu.healthcheck;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class HealthCheckController {

	@GetMapping("/healthcheck")
	public HealthResponse healthCheck() {
		return new HealthResponse("UP", List.of("Янковский Э.", "Мелещенко С.", "Гордиенко Ю.", "Волнухина В."));
	}

	// DTO для ответа
	public record HealthResponse(String status, List<String> authors) {
	}
}
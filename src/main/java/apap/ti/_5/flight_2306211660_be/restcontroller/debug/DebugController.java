package apap.ti._5.flight_2306211660_be.restcontroller.debug;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DebugController {

    @GetMapping("/api/debug/version")
    public Map<String, String> getVersion() {
        // Change this value each time you want to confirm a new deploy
        return Map.of(
            "versionMarker", "bill-v5-api-key-attached"
        );
    }
}

package apap.ti._5.flight_2306211660_be.restcontroller.debug;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

    @GetMapping("/api/debug/version")
    public Map<String, String> getVersion() {
        // Change this value each time you want to confirm a new deploy
        return Map.of(
            "versionMarker", "bill-v6"
        );
    }
}

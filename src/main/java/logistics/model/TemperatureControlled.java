package logistics.model;

/**
 * Sicaklik kontrolü yapabilen sogutuculu araçlar için odaklanmis yetenek arayüzü (ISP).
 */
public interface TemperatureControlled {
    double getTargetTemperatureCelsius();
}

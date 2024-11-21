package sleep.service;

import sleep.dto.RegisterDto;
import sleep.models.User;

public interface AuthService {
    void register(RegisterDto registerDto);
}

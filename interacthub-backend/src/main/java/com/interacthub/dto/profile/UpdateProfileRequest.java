package com.interacthub.dto.profile;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
public record UpdateProfileRequest(@Size(max=100) String displayName, @Size(max=500) String bio, LocalDate dateOfBirth, @Size(max=20) String gender, @Size(max=200) String address) {}

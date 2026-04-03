package com.skillshare.skillshare.dto.exchange;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MessageCreateDTO {

    @NotBlank(message = "Message cannot be empty")
    @Size(max = 1000, message = "Message must be 1000 characters or less")
    private String content;

    public MessageCreateDTO() {}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

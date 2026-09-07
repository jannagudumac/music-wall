package com.musicwall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class MusicSectionDTO {

    private Long id;

    @NotBlank(message = "Section name is required")
    @Size(max = 80, message = "Section name is too long")
    private String name;

    @Pattern(
            regexp = "CREAM|ROSE|PEACH|MINT|SKY|LAVENDER",
            message = "Unknown note color"
    )
    private String noteColor = "CREAM";

    private List<MusicItemDTO> items = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNoteColor() {
        return noteColor;
    }

    public void setNoteColor(String noteColor) {
        this.noteColor = noteColor;
    }

    public List<MusicItemDTO> getItems() {
        return items;
    }

    public void setItems(List<MusicItemDTO> items) {
        this.items = items;
    }
}

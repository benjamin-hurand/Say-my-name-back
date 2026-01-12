package com.saymyname.webapp.dto.gamemode;

import java.util.List;

public final class GameModeAdminDtos {
        private GameModeAdminDtos() {
        }

        // Write
        public record GameModeAttributeWriteDto(Long id, Long attributeId) {
        }

        public record CreateGameModeRequestDto(
                        String title,
                        String description,
                        String operator, // "AND"|"OR"
                        List<GameModeAttributeWriteDto> attributes) {
        }

        public record UpdateGameModeRequestDto(
                        Long id,
                        String title,
                        String description,
                        String operator,
                        List<GameModeAttributeWriteDto> attributes) {
        }

        // Read
        public record GameModeAttributeResponseDto(Long id, Long attributeId) {
        }

        public record GameModeResponseDto(
                        Long id,
                        String title,
                        String description,
                        String operator,
                        List<GameModeAttributeResponseDto> attributes) {
        }
}

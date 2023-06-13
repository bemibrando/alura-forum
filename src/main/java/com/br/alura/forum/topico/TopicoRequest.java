package br.com.alura.forum.topico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TopicoRequest(
        @NotBlank
        @NotNull
        String titulo,
        @NotNull
        @NotBlank
        String mensagem,
        @NotNull
        @NotBlank
        String curso) {
}

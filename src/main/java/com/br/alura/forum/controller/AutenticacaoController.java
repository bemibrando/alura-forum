package com.filiperobot.aluraforumapi.controller;

import com.filiperobot.aluraforumapi.domain.user.DTO.DadosLogin;
import com.filiperobot.aluraforumapi.infra.exceptions.DTO.DadosErrosValidacao;
import com.filiperobot.aluraforumapi.infra.security.DTO.DadosTokenJWT;
import com.filiperobot.aluraforumapi.domain.user.Usuario;
import com.filiperobot.aluraforumapi.infra.security.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoint para gerar token/autenticação na aplicação")
public class AutenticacaoController {

    private final AuthenticationManager authenticationManager;

    private final TokenService tokenService;

    @PostMapping
    @Operation(summary = "Fazer login na aplicação", description = "Usando um email e senha já cadastrados, gerar um token para autenticação. " +
            "O Token gerado tem duração de 2 horas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retorna o tipo e o token gerado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DadosTokenJWT.class))
            }),
            @ApiResponse(responseCode = "400", description = "Retorna um lista com as informações dos capos inválidos", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DadosErrosValidacao.class)))
            }),
            @ApiResponse(responseCode = "403", description = "Bloqueia a requisição caso o usuário não seja autorizado " +
                    "ou não foi encontrado/cadastrado", content = {@Content(schema = @Schema())})
    })
    public ResponseEntity<DadosTokenJWT> login(@RequestBody @Valid DadosLogin dadosLogin) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(dadosLogin.email(), dadosLogin.senha());

        var authentication = authenticationManager.authenticate(authenticationToken);

        String tokenJWT = tokenService.gerarToken((Usuario) authentication.getPrincipal());

        return ResponseEntity.ok(new DadosTokenJWT("Bearer", tokenJWT));
    }
}

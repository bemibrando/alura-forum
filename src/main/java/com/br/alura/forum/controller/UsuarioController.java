package com.filiperobot.aluraforumapi.controller;

import com.filiperobot.aluraforumapi.domain.user.*;
import com.filiperobot.aluraforumapi.domain.user.DTO.DadosCadastroUsuario;
import com.filiperobot.aluraforumapi.domain.user.DTO.DadosUsuarioAtualizar;
import com.filiperobot.aluraforumapi.domain.user.DTO.DadosUsuarioCompleto;
import com.filiperobot.aluraforumapi.domain.user.DTO.DadosListagemUsuario;
import com.filiperobot.aluraforumapi.infra.exceptions.DTO.DadosErrosValidacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoint para gerenciar usuários, criação, busca, listagem, atualização e remoção")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    @Transactional
    @Operation(summary = "Cadastrar um usuário", description = "Cadastra um usuário no banco de dados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Retorna os dados do usuário cadastrado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DadosUsuarioCompleto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Retorna um lista com as informações dos capos inválidos", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DadosErrosValidacao.class)))
            }),
            @ApiResponse(responseCode = "403", description = "Bloqueia a requisição caso não seja enviado nada no corpo da requisição, ou seja um email já cadastrado",
                    content = {@Content(schema = @Schema())})
    })
    public ResponseEntity<DadosUsuarioCompleto> cadastrar(
            @RequestBody @Valid DadosCadastroUsuario dadosUsuario, UriComponentsBuilder uriBuilder) {

        String senhaCriptografada = passwordEncoder.encode(dadosUsuario.senha());
        var novosDados = new DadosCadastroUsuario(dadosUsuario, senhaCriptografada);

        var usuario = usuarioRepository.save(new Usuario(novosDados));

        var uri = uriBuilder.path("/usuario/{id}").buildAndExpand(usuario.getId()).toUri();

        return ResponseEntity.created(uri).body(new DadosUsuarioCompleto(usuario));
    }

    @GetMapping("{id}")
    @Operation(summary = "Buscar usuário", description = "Procura um usuário com o ID informado no banco de dados.",
            security = @SecurityRequirement(name = "TokenJWT"))
    @Parameters(value = {
            @Parameter(name = "id", required = true, description = "Id do usuário a ser encontrado no banco de dados")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retorna os dados do usuário encontrado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DadosUsuarioCompleto.class))
            }),
            @ApiResponse(responseCode = "403", description = "Bloqueia a requisição caso o token não seja valido ou não foi enviado",
                    content = {@Content(schema = @Schema())})
    })
    public ResponseEntity<DadosUsuarioCompleto> usuario(@PathVariable Long id) {
        var usuario = usuarioRepository.getReferenceById(id);
        return ResponseEntity.ok(new DadosUsuarioCompleto(usuario));
    }

    @GetMapping
    @Operation(summary = "Listar usuários", description = "Busca todos os usuários do banco de dados, e mostra uma lista com suas informações.",
            security = @SecurityRequirement(name = "TokenJWT"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retorna uma lista com os dados dos usuários encontrados", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DadosListagemUsuario.class)))
            }),
            @ApiResponse(responseCode = "403", description = "Bloqueia a requisição caso o token não seja valido ou não foi enviado",
                    content = {@Content(schema = @Schema())})
    })
    public ResponseEntity<List<DadosListagemUsuario>> listarUsuarios() {
        var usuarios = usuarioRepository
                .findAll()
                .stream()
                .map(DadosListagemUsuario::new)
                .toList();
        return ResponseEntity.ok(usuarios);
    }

    @PutMapping
    @Transactional
    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados de um usuário",
            security = @SecurityRequirement(name = "TokenJWT"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retorna os novos dados do usuário atualizado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DadosUsuarioCompleto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Retorna uma lista com as informações dos capos inválidos", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DadosErrosValidacao.class)))
            }),
            @ApiResponse(responseCode = "403", description = "Bloqueia a requisição caso o token não seja valido ou não foi enviado",
                    content = {@Content(schema = @Schema())})
    })
    public ResponseEntity<DadosUsuarioCompleto> atualizar(@RequestBody @Valid DadosUsuarioAtualizar dadosUsuarioAtualizacao) {
        var usuario = usuarioRepository.getReferenceById(dadosUsuarioAtualizacao.id());

        if (dadosUsuarioAtualizacao.senha() != null) {
            String senhaCriptografada = passwordEncoder.encode(dadosUsuarioAtualizacao.senha());
            var UsuarioAtualizadoComSenha = new DadosUsuarioAtualizar(dadosUsuarioAtualizacao, senhaCriptografada);
            usuario.atualizar(UsuarioAtualizadoComSenha);
        } else {
            usuario.atualizar(dadosUsuarioAtualizacao);
        }

        return ResponseEntity.ok(new DadosUsuarioCompleto(usuario));
    }

    @DeleteMapping("{id}")
    @Transactional
    @Operation(summary = "Remover usuário", description = "Remove do banco de dados o usuário com o ID informado",
            security = @SecurityRequirement(name = "TokenJWT"))
    @Parameters(value = {
            @Parameter(name = "id", required = true, description = "Id do usuário a ser removido do banco de dados")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Caso a exclusão/remoção do usuário seja feita com sucesso",
                    content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", description = "Bloqueia a requisição caso o token não seja valido ou não foi enviado, " +
                    "também caso o Id informado não é válido ou não foi encontrado",
                    content = {@Content(schema = @Schema())})
    })
    public ResponseEntity<Void> removerUsuario(@PathVariable Long id) {
        usuarioRepository.findById(id).ifPresentOrElse(
                usuarioRepository::delete,
                () -> {
                    throw new IllegalArgumentException("Usuário não existe, não é possível deleta-lo");
                }
        );

        return ResponseEntity.noContent().build();
    }
}

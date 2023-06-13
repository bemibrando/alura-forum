package br.com.alura.forum.usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioResponse saveUsuario(UsuarioRequest usuarioRequest) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(16);
        Usuario usuario = new Usuario(usuarioRequest);
        usuario.setSenha(encoder.encode(usuarioRequest.senha()));
        return new UsuarioResponse(usuarioRepository.save(usuario));
    }
}

package br.com.alura.forum.topico;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/topicos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-key")
public class TopicoController {

    private final TopicoService topicoService;
    @PostMapping
    public ResponseEntity<TopicoResponse> saveTopico(@Valid @RequestBody TopicoRequest topicoRequest) {
        return new ResponseEntity<>(topicoService.saveTopico(topicoRequest),HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<TopicoResponse>> getTopicos(Pageable pageable) {
        return ResponseEntity.ok(topicoService.getTopicos(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TopicoResponse> getTopicoById(@PathVariable Long id){
        return ResponseEntity.ok(topicoService.getTopicoById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TopicoResponse> updateTopicoById(@PathVariable Long id, @RequestBody AlteredTopic alteredTopic){
        return ResponseEntity.ok(topicoService.updateTopicoById(id,alteredTopic));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id){
        topicoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

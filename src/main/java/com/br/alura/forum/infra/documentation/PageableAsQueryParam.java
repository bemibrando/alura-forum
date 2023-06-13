package com.filiperobot.aluraforumapi.infra.documentation;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Parameters(value = {
        @Parameter(in = ParameterIn.QUERY, name = "pagina", description = "Número da pagina a ser visualizada", content = {
                @Content(schema = @Schema(type = "integer", defaultValue = "0"))
        }),
        @Parameter(in = ParameterIn.QUERY, name = "tamanho", description = "Número máximo de cursos por pagina", content = {
                @Content(schema = @Schema(type = "integer", defaultValue = "10"))
        }),
        @Parameter(in = ParameterIn.QUERY, name = "ordem", description = "Lista com o nome dos atributos a serem " +
                "utilizados para ordenar os cursos mostrados", content = {
                @Content(array = @ArraySchema(schema = @Schema(type = "string", nullable = true)))
        }, allowEmptyValue = true)
})
public @interface PageableAsQueryParam {
}

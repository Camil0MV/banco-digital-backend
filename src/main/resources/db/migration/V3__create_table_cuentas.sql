-- V3__create_table_cuentas.sql
-- Registra formalmente la tabla cuentas que fue creada manualmente antes del baseline de Flyway.
-- Esta migración documenta el esquema real existente en producción para mantener consistencia
-- entre el historial de migraciones y el estado de la base de datos.

CREATE TABLE IF NOT EXISTS public.cuentas (
    id_cuenta        UUID          NOT NULL DEFAULT gen_random_uuid(),
    tipo_doc_dueno   INTEGER       NOT NULL,
    num_doc_dueno    VARCHAR(20)   NOT NULL,
    id_tipo_cuenta   INTEGER       NOT NULL,
    id_estado_cuenta INTEGER       NOT NULL,
    saldo            NUMERIC(18,2) NOT NULL DEFAULT 0,
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP,

    CONSTRAINT pk_cuentas PRIMARY KEY (id_cuenta),
    CONSTRAINT fk_cuenta_usuario
        FOREIGN KEY (tipo_doc_dueno, num_doc_dueno)
        REFERENCES public.usuarios (id_tipo_doc, numero_documento),
    CONSTRAINT fk_cuenta_tipo
        FOREIGN KEY (id_tipo_cuenta)
        REFERENCES public.tipos_cuenta (id_tipo_cuenta),
    CONSTRAINT fk_cuenta_estado
        FOREIGN KEY (id_estado_cuenta)
        REFERENCES public.estados_cuenta (id_estado_cuenta)
);
-- Core non-destructive indexes for query performance.
-- Uses IF NOT EXISTS so it is safe on environments where indexes already exist.

-- usuarios: common filter by role
CREATE INDEX IF NOT EXISTS idx_usuarios_id_rol
    ON public.usuarios (id_rol);

-- cuentas: owner composite FK used by lookups and joins
CREATE INDEX IF NOT EXISTS idx_cuentas_dueno_doc
    ON public.cuentas (tipo_doc_dueno, num_doc_dueno);

-- cuentas: foreign key lookups to catalogs
CREATE INDEX IF NOT EXISTS idx_cuentas_id_tipo_cuenta
    ON public.cuentas (id_tipo_cuenta);

CREATE INDEX IF NOT EXISTS idx_cuentas_id_estado_cuenta
    ON public.cuentas (id_estado_cuenta);

-- transacciones: frequent query by origin account and date range
CREATE INDEX IF NOT EXISTS idx_transacciones_origen_fecha
    ON public.transacciones (id_cuenta_origen, fecha);

-- transacciones: destination account lookups
CREATE INDEX IF NOT EXISTS idx_transacciones_destino_fecha
    ON public.transacciones (id_cuenta_destino, fecha);

-- transacciones: foreign key lookups by transaction type
CREATE INDEX IF NOT EXISTS idx_transacciones_id_tipo_transaccion
    ON public.transacciones (id_tipo_transaccion);

-- Permite registrar usuarios sin segundo apellido.
ALTER TABLE public.usuarios
    ALTER COLUMN segundo_apellido DROP NOT NULL;
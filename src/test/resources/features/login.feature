Feature: Login de usuario

    Scenario: Login exitoso
        Given el usuario quiere iniciar sesion
        When envia credenciales validas "juan@example.com" y "Abc123#@"
        Then el login debe responder con estado 200
        And el login debe retornar un token


    Scenario Outline: Login fallido
        Given el usuario quiere iniciar sesion
        When envia credenciales invalidas "<correo>" y "<contrasena>"
        Then el login debe responder con estado <estado>

    Examples:
    | correo              | contrasena | estado |
    | juan@gmail.com      | mala123    | 404    |
    | correo-invalido     | Abc123#@   | 400    |
    |                     | Abc123#@   | 400    |
    | juan@gmail.com      |             | 400    |
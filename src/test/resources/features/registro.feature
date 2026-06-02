Feature: Registro de usuario

  Scenario: Registro exitoso de cliente
    Given el usuario quiere registrarse
    When envia un registro valido dinamico
    Then el sistema debe responder con estado 201

  Scenario Outline: Registro fallido por datos invalidos
    Given el usuario quiere registrarse
    When envia un registro con "<correo>" y "<documento>"
    Then el sistema debe responder con estado <estado>

    Examples:
      | correo          | documento | estado |
      | correo-invalido | 123456789 | 400    |
      |                 | 123456789 | 400    |
      | test@test.com   |           | 400    |
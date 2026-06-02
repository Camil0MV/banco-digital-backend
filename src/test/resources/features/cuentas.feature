Feature: Gestion de cuentas bancarias

  Scenario: Consultar saldo disponible de una cuenta de manera exitosa
    Given el usuario se autentica con el correo "usuario.prueba@bancodigital.com" y la contrasena "Password456!"
    When solicita el saldo de la cuenta "4f61d91a-d48d-4ad5-a5e8-2ad95cb5c0f4"
    Then el sistema financiero debe responder con estado 200
  
  Scenario: Intentar consultar saldo de una cuenta sin estar autenticado
    Given el usuario no se encuentra autenticado en el sistema
    When solicita el saldo de la cuenta "4f61d91a-d48d-4ad5-a5e8-2ad95cb5c0f4"
    Then el sistema financiero debe responder con estado 401
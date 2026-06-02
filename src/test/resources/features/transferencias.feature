Feature: Transferencias bancarias

  Scenario: Realizar transferencia exitosa entre cuentas
    Given el usuario tiene fondos en su cuenta para transferir
    When realiza una transferencia desde "4f61d91a-d48d-4ad5-a5e8-2ad95cb5c0f4" hacia "032f3cb9-3925-47cc-8eb8-0bded3f52461" por valor de 100
    Then el sistema de transferencias debe responder con estado 201

  Scenario Outline: Transferencias fallidas por datos inválidos y excepcionales
    Given el usuario tiene fondos en su cuenta para transferir
    When realiza una transferencia desde "<cuenta_origen>" hacia "<cuenta_destino>" por valor de "<monto>"
    Then el sistema de transferencias debe responder con estado <estado_esperado>

    Examples:
      | cuenta_origen                        | cuenta_destino                       | monto  | estado_esperado |
      | 4f61d91a-d48d-4ad5-a5e8-2ad95cb5c0f4 | 032f3cb9-3925-47cc-8eb8-0bded3f52461 | -50000 | 400             |
      | 4f61d91a-d48d-4ad5-a5e8-2ad95cb5c0f4 | 032f3cb9-3925-47cc-8eb8-0bded3f52461 | letras | 400             |
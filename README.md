# Calcula Fácil (Easy Calc)

Aplicativo Android de cálculos rápidos, com interface em português e identidade visual em tons de azul. A primeira ferramenta é o cálculo de IMC; um segundo item já aparece no menu como “Em desenvolvimento”.

## O que já está implementado

- Tela inicial com acesso ao IMC, item de próxima funcionalidade e configurações.
- Formulário de IMC com peso em quilogramas e altura em centímetros. Aceita vírgula ou ponto como separador decimal, valida os dois valores e habilita **Calcular** apenas quando ambos são válidos.
- Resultado com IMC e categoria adulta: abaixo do peso, peso ideal, acima do peso e obesidade classes I, II e III.
- Ação **Calcular novamente**, que limpa os campos, e retorno ao menu pela seta superior ou pelo botão Voltar do Android.
- Preferência de tema claro, escuro ou conforme o dispositivo, salva localmente.
- Rodapé “Desenvolvido por Rafael Luis - 2026”.

## Abrir e compilar

1. Abra esta pasta no Android Studio com suporte a Android Gradle Plugin 8.13.2.
2. Instale o Android SDK Platform 36 quando solicitado e aguarde a sincronização do Gradle.
3. Execute a configuração `app` em um emulador ou dispositivo Android.

O app usa `minSdk 23`, `compileSdk 36` e `targetSdk 36`. O `targetSdk` atende ao requisito anunciado pelo Google Play para novos apps enviados a partir de 31 de agosto de 2026. Para publicar, ainda será necessário preparar a ficha da loja, capturas de tela, política de privacidade conforme o tratamento de dados adotado e um pacote assinado (`.aab`) no Play Console.

## Estrutura

- `app/src/main/java/br/com/rafaelluis/easycalc/MainActivity.kt`: navegação, telas, temas e cálculo de IMC.
- `app/src/main/AndroidManifest.xml`: configuração do app Android e atividade inicial.
- `app/build.gradle.kts`: SDKs, identificador e dependências do app.

## IMC e limites

O cálculo é `peso (kg) / altura (m)²`. As categorias adultas seguem as faixas publicadas pela Organização Mundial da Saúde: abaixo de 18,5; 18,5–24,9; 25,0–29,9; 30,0–34,9; 35,0–39,9; e 40,0 ou mais. O resultado é apresentado como referência, não como diagnóstico. Os pontos de corte adultos não se aplicam da mesma forma a crianças e adolescentes.

Fontes: [OMS — Nutrition for a healthy life](https://www.who.int/europe/news-room/fact-sheets/item/nutrition---maintaining-a-healthy-lifestyle) · [Google Play — requisito de nível de API](https://developer.android.com/google/play/requirements/target-sdk)

# Calcula Fácil (Easy Calc)

Aplicativo Android de cálculos rápidos, com interface em português brasileiro e inglês e identidade visual em tons de azul. A primeira ferramenta é o cálculo de IMC; um segundo item já aparece no menu como “Em desenvolvimento”.

## O que já está implementado

- Tela inicial com acesso ao IMC, item de próxima funcionalidade e configurações.
- Idioma selecionável entre português brasileiro, inglês e o idioma do dispositivo. No modo automático, idiomas diferentes de pt-BR e inglês usam inglês.
- Unidade de medida selecionável entre métrico (padrão) e imperial.
- Formulário de IMC métrico com peso em quilogramas e altura em centímetros; no modo imperial, peso em libras e altura em pés e polegadas. Aceita vírgula ou ponto como separador decimal e só habilita **Calcular** com valores válidos.
- Resultado com IMC e categoria adulta, além de uma tabela com as faixas de peso correspondentes à altura informada, convertida para a unidade selecionada.
- Ação **Calcular novamente**, que limpa os campos, e retorno ao menu pela seta superior ou pelo botão Voltar do Android.
- Preferências de tema e unidade salvas localmente; idioma integrado às preferências de idioma do Android.
- Botão **Sobre / About** nas configurações com os créditos do aplicativo.

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

No sistema métrico, o cálculo é `peso (kg) / altura (m)²`; no imperial, é `peso (lb) × 703 / altura (in)²`. As categorias adultas seguem os limites de IMC: abaixo de 18,5; 18,5–<25; 25–<30; 30–<35; 35–<40; e 40 ou mais. A tabela converte esses limites em faixas de peso na unidade selecionada para a altura informada. O resultado é apresentado como referência, não como diagnóstico. Os pontos de corte adultos não se aplicam da mesma forma a crianças e adolescentes.

Fontes: [OMS — Nutrition for a healthy life](https://www.who.int/europe/news-room/fact-sheets/item/nutrition---maintaining-a-healthy-lifestyle) · [CDC — fórmula do IMC métrico e imperial](https://www.cdc.gov/growth-chart-training/hcp/using-bmi/calculating-bmi.html) · [Google Play — requisito de nível de API](https://developer.android.com/google/play/requirements/target-sdk) · [Android — idiomas por aplicativo](https://developer.android.com/guide/topics/resources/app-languages)

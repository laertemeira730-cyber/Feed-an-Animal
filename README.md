# Lançar Comidas para Animais — Etapa 1

Base inicial para Minecraft Java 1.20.1 + Minecraft Forge 47.4.28 (Java 17).

## O que já está implementado

- **Barra de Treinamento**: bloco com `BlockEntity`. Ao clicar com o botão direito, concede +1 de força de arremesso, com intervalo de 1 segundo. A força está limitada provisoriamente a 100 e é salva nos dados persistentes do jogador, inclusive após a morte.
- **Delimitador de Cercado**: clique com o botão direito no primeiro bloco e depois no segundo bloco do canto oposto para criar o volume do cercado.
- **Delimitador de Vendas**: usa o mesmo processo, mas grava uma área de vendas separada.
- As áreas pertencem ao jogador, cada jogador pode ter uma área ativa de cada tipo em cada dimensão, e tudo fica salvo nos dados do mundo (`SavedData`).
- Os três blocos aparecem na aba criativa própria do mod e têm modelos, traduções e loot tables básicos.

## Como marcar uma área

1. Coloque dois blocos do mesmo tipo nos cantos opostos da área.
2. Clique com o botão direito no primeiro marcador.
3. Clique com o botão direito no segundo marcador.
4. O jogo confirma as coordenadas e o volume delimitado.

Cada jogador tem uma área ativa de cercado e uma área ativa de vendas por dimensão. Para redefinir uma área, basta clicar nos dois marcadores novamente, começando pelo primeiro canto.

## Como obter os blocos rapidamente

```text
/give @p feedanimals:training_bar
/give @p feedanimals:pen_marker 2
/give @p feedanimals:sales_marker 2
```

## Como testar

Abra a pasta do projeto no IntelliJ IDEA ou Eclipse com suporte a Gradle, usando JDK 17.

- `gradle runClient` — iniciar o cliente de desenvolvimento.
- `gradle build` — compilar o mod; o JAR fica em `build/libs/`.

## Observações desta etapa

- O treino ainda usa um clique por sessão; a animação de exercício e o sistema de pressionar/segurar serão adicionados em etapa posterior.
- A área é registrada e persistida, mas ainda não restringe a movimentação dos animais nem executa vendas. Isso será ligado aos sistemas de animais e economia na próxima etapa.
- A força máxima e o ganho por treino são valores iniciais de teste, não valores definitivos do jogo Roblox.
- Os marcadores usam modelos geométricos simples e texturas vanilla temporárias.

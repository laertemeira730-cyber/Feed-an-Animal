# Lançar Comidas para Animais — Base integrada

Mod para Minecraft Java 1.20.1 + Minecraft Forge 47.4.28 (Java 17), inspirado na experiência Roblox Throw Food For Animals.

## Sistemas incluídos nesta base

- **Animais vanilla**: usa os próprios mobs e modelos do Minecraft. Ao lançar um alimento próximo de um animal, ele pode ser atraído e convertido em um registro de animal no inventário.
- **Alimentos arremessáveis**: três categorias iniciais (básica, rara e premium), com alcances de atração diferentes e ícones baseados em texturas vanilla.
- **Registros de animais**: use um registro dentro da área delimitada do cercado para trazer de volta o animal original.
- **Cercado**: dois marcadores invisíveis definem os cantos opostos da área. Os animais dentro da área geram moedas periodicamente.
- **Vendas**: dois marcadores invisíveis definem a área de vendas. O menu permite vender um ou todos os registros de animais guardados na mochila.
- **Loja de alimentos**: use o item Loja de Alimentos para comprar os alimentos com o saldo do jogador.
- **Treinamento**: clique com o botão direito na barra para abrir o menu e treinar manualmente. Use Ctrl + botão direito na barra para iniciar o treinamento automático; pressione Shift para parar.
- **Fases**: configure a quantidade usando o item Delimitador de Fases, marque os dois extremos e acompanhe as fases na tela do marcador. Cada fase tem sua própria aba e conclusão persistente.
- **Persistência**: saldo, força, áreas, projetos e fases concluídas são salvos por jogador/mundo.

## Como começar

Ao entrar pela primeira vez, o jogador recebe 8 alimentos básicos para testar o arremesso. O saldo inicial está centralizado em `EconomyRules.STARTING_MONEY` e permanece em zero nesta base enquanto o valor final de início não for definido.

1. Coloque a Barra de Treinamento e clique nela para abrir o menu.
2. Use a Loja de Alimentos para comprar alimentos quando tiver moedas.
3. Lance alimentos perto dos animais. Quando um animal for atraído, seu registro irá para o inventário.
4. Coloque dois Delimitadores de Cercado nos cantos opostos e clique com o botão direito em cada marcador para definir a área.
5. Use o registro de animal em um bloco dentro do cercado para colocar o animal.
6. Coloque dois Delimitadores de Vendas nos cantos opostos e marque a área. Abra o menu do delimitador com Shift + botão direito no marcador ou usando o item no ar para vender animais guardados na mochila.

## Controles dos menus

- **Barra de Treinamento**: botão direito abre o menu; Ctrl + botão direito inicia o treino automático; Shift interrompe.
- **Delimitador de Cercado/Vendas**: usar o item no ar abre o menu; clique direito normal nos marcadores escolhe os cantos; Shift + clique direito no marcador abre o menu.
- **Loja de Alimentos**: botão direito no item abre a loja.
- **Delimitador de Fases**: botão direito no ar abre a configuração; clique no bloco marcador abre o painel das fases.
- **Animais**: clique direito com a mão vazia ou item que não seja alimento abre as informações do mob. Comida do mod continua sendo arremessada.

## Valores de balanceamento

Os preços, alcance dos alimentos, renda do cercado e frequência de renda ficam centralizados em `EconomyRules`. São parâmetros iniciais para teste e podem ser ajustados sem espalhar valores pelo código.

## Compilar

Use JDK 17 e Gradle 8.1.1:

```text
gradle build --no-daemon
```

O JAR fica em `build/libs/`. O workflow do GitHub Actions também compila automaticamente a cada push na branch `main`.

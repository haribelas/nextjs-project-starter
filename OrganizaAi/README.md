# Organiza Aí - Aplicativo de Gerenciamento de Tarefas

Um aplicativo Android completo desenvolvido em **Kotlin** para gerenciamento de tarefas com notificações automáticas, armazenamento local e suporte a temas claro/escuro.

## 📱 Funcionalidades

### ✅ Principais Recursos
- **Lista de Tarefas**: Visualização organizada de todas as tarefas
- **Adicionar/Editar Tarefas**: Formulário completo com validação
- **Categorização**: Organização por categorias (Trabalho, Pessoal, Estudos, etc.)
- **Notificações Automáticas**: Lembretes 1 hora antes do prazo
- **Armazenamento Local**: Banco de dados Room para persistência
- **Tema Adaptativo**: Suporte automático a tema claro/escuro
- **Interface Responsiva**: Layout adaptável para diferentes tamanhos de tela

### 🎯 Detalhes das Funcionalidades
- **Campos da Tarefa**: Título, descrição, data/hora e categoria
- **Status Visual**: Indicadores de prioridade e status (pendente, concluída, atrasada)
- **Ações Rápidas**: Marcar como concluída, editar, excluir
- **Filtros**: Visualização por categoria e status
- **Estatísticas**: Contadores de tarefas pendentes e concluídas

## 🏗️ Arquitetura

### 📦 Estrutura de Pacotes
```
com.organiza.ai/
├── ui/           # Interface do usuário (Activities, Adapters)
├── data/         # Camada de dados (DAO, Database, Repository)
├── model/        # Classes de modelo (Entities)
├── viewmodel/    # ViewModels (Lógica de apresentação)
└── utils/        # Utilitários (Notificações, Alarmes)
```

### 🔧 Tecnologias Utilizadas
- **Linguagem**: Kotlin 100%
- **Banco de Dados**: Room Database
- **Arquitetura**: MVVM (Model-View-ViewModel)
- **UI**: Material Design 3
- **Notificações**: AlarmManager + NotificationManager
- **Async**: Coroutines + LiveData
- **Injeção de Dependência**: Manual (Repository Pattern)

## 🚀 Como Executar

### 📋 Pré-requisitos
- Android Studio Arctic Fox ou superior
- SDK Android 24+ (Android 7.0)
- Kotlin 1.9.10+
- Gradle 8.1.2+

### 🔧 Configuração
1. **Clone o projeto**:
   ```bash
   git clone <repository-url>
   cd OrganizaAi
   ```

2. **Abra no Android Studio**:
   - File → Open → Selecione a pasta do projeto

3. **Sync do Gradle**:
   - O Android Studio fará o sync automaticamente
   - Aguarde o download das dependências

4. **Execute o app**:
   - Conecte um dispositivo Android ou inicie um emulador
   - Clique em "Run" ou pressione Shift+F10

### 📱 Requisitos do Dispositivo
- **Android 7.0** (API 24) ou superior
- **Permissões necessárias**:
  - Notificações
  - Alarmes exatos
  - Despertar dispositivo

## 📚 Componentes Principais

### 🗃️ Banco de Dados (Room)
```kotlin
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val dateTime: Date,
    val category: String,
    val isCompleted: Boolean = false
)
```

### 🔔 Sistema de Notificações
- **AlarmHelper**: Agenda alarmes para lembretes
- **NotificationHelper**: Cria e exibe notificações
- **ReminderBroadcast**: Processa alarmes e ações

### 🎨 Interface do Usuário
- **MainActivity**: Tela principal com lista de tarefas
- **TaskFormActivity**: Formulário para criar/editar tarefas
- **TaskAdapter**: Adapter do RecyclerView com ViewBinding

### 📊 Gerenciamento de Estado
- **TaskViewModel**: Lógica de negócio e estado da UI
- **TaskRepository**: Abstração da camada de dados
- **LiveData**: Observação reativa de mudanças

## 🎨 Design e UX

### 🌈 Temas
- **Tema Claro**: Cores vibrantes com boa legibilidade
- **Tema Escuro**: Cores suaves para uso noturno
- **Adaptação Automática**: Segue configuração do sistema

### 📐 Layout Responsivo
- **Material Design 3**: Componentes modernos e acessíveis
- **Cards**: Organização visual das tarefas
- **FAB**: Botão flutuante para ação principal
- **Chips**: Categorias visuais
- **Indicadores**: Status visual das tarefas

### 🎯 Experiência do Usuário
- **Navegação Intuitiva**: Fluxo simples e direto
- **Feedback Visual**: Animações e transições suaves
- **Validação**: Mensagens claras de erro e sucesso
- **Acessibilidade**: Suporte a leitores de tela

## 🔧 Configurações Avançadas

### ⚡ Performance
- **ProGuard**: Otimização de código para release
- **R8**: Compilação otimizada
- **ViewBinding**: Binding seguro de views
- **DiffUtil**: Atualizações eficientes de lista

### 🛡️ Segurança
- **Validação de Entrada**: Sanitização de dados
- **Permissões**: Solicitação adequada de permissões
- **Backup**: Configuração de backup automático

### 📊 Monitoramento
- **Logs**: Sistema de logging estruturado
- **Crash Handling**: Tratamento de exceções
- **Performance**: Otimizações de memória e CPU

## 🧪 Testes

### 🔍 Estratégia de Testes
- **Unit Tests**: Testes de lógica de negócio
- **Integration Tests**: Testes de banco de dados
- **UI Tests**: Testes de interface (Espresso)

### 📝 Executar Testes
```bash
# Testes unitários
./gradlew test

# Testes instrumentados
./gradlew connectedAndroidTest

# Todos os testes
./gradlew check
```

## 📦 Build e Deploy

### 🏗️ Build de Desenvolvimento
```bash
./gradlew assembleDebug
```

### 🚀 Build de Produção
```bash
./gradlew assembleRelease
```

### 📱 Instalação
```bash
# Debug
adb install app/build/outputs/apk/debug/app-debug.apk

# Release
adb install app/build/outputs/apk/release/app-release.apk
```

## 🤝 Contribuição

### 📋 Guidelines
1. **Fork** o repositório
2. **Crie** uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. **Commit** suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. **Push** para a branch (`git push origin feature/AmazingFeature`)
5. **Abra** um Pull Request

### 📝 Padrões de Código
- **Kotlin Style Guide**: Seguir convenções oficiais
- **Comentários**: Documentar código complexo
- **Commits**: Mensagens descritivas e claras
- **Testes**: Incluir testes para novas funcionalidades

## 📄 Licença

Este projeto está licenciado sob a **MIT License** - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 👥 Autores

- **Desenvolvedor Principal** - *Trabalho inicial* - [Seu Nome](https://github.com/seuusuario)

## 🙏 Agradecimentos

- **Material Design**: Guidelines de design
- **Android Developers**: Documentação e exemplos
- **Kotlin Team**: Linguagem moderna e expressiva
- **Comunidade Open Source**: Bibliotecas e ferramentas

## 📞 Suporte

Para suporte, envie um email para suporte@organizaai.com ou abra uma issue no GitHub.

---

**Organiza Aí** - Organize sua vida, uma tarefa de cada vez! 📝✨

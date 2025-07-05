# 🌱 ReVerde

ReVerde é um aplicativo inovador voltado para o rastreamento de hábitos sustentáveis, desenvolvido para pessoas que desejam construir um futuro melhor por meio de pequenas ações diárias. A proposta do sistema é tornar a sustentabilidade algo acessível, recompensador e integrado à rotina do usuário.

---

## 📲 Funcionalidades Principais

O ReVerde oferece uma experiência simples e motivadora para quem deseja manter hábitos sustentáveis. Abaixo estão as principais telas e funcionalidades do sistema:

- **Check-list de hábitos**: registrar ações como reciclagem, economia de água e descarte correto de resíduos.
- **Tela de perfil**: visualização de dados do usuário e acesso à tela de certificados.
- **Conversão de eco-pontos em certificados digitais**.
- **Tela de estatísticas**: visão semanal e mensal do desempenho sustentável.
- **Página de boas-vindas/apresentação** do projeto.

---

## ✅ Requisitos Funcionais

- Registrar hábitos sustentáveis  
- Validar hábitos registrados  
- Creditar eco-pontos  
- Verificar saldo de eco-pontos  
- Converter eco-pontos em certificados  

---

## 🔄 Mudanças e Melhorias no Sistema

Durante o processo de desenvolvimento, algumas alterações foram feitas além do MVP original:

- **Banco de dados**: Adicionada a coluna `resposta_incorreta` na tabela `perguntavalidacao`, aprimorando a lógica de feedback.
- **Melhorias na conversão de eco-pontos**: agora o usuário pode visualizar o certificado atual e baixá-lo para sua máquina.
- **Bloqueio de hábitos**: agora também ocorre quando o usuário responde incorretamente às perguntas de validação.
- **Tela de estatísticas**: visão semanal e mensal do progresso com gráficos intuitivos.
- **Tela de apresentação**: explicação introdutória do propósito do ReVerde.

---

## 🧪 Estratégia de Testes

Adotamos a metodologia **TDD (Test-Driven Development)** como base para a construção do sistema, priorizando qualidade e manutenção desde as primeiras linhas de código. O ciclo *Red – Green – Refactor* foi seguido durante toda a implementação das funcionalidades.

### 📦 Ferramentas e Bibliotecas Utilizadas

- **JUnit 5** (`org.junit.jupiter.api.*`): criação de testes unitários.
- **Mockito** (`org.mockito.*`): simulação de comportamentos para testes isolados.
- **JavaFX** (`FXMLLoader`, `Parent`, `Scene`, `Stage`): componentes fundamentais para testes de UI.

---

## 📅 O que foi feito na última semana

Na reta final de desenvolvimento, as seguintes tarefas foram executadas:

- **Randomização das perguntas** de validação e alimentação do banco de dados.
- **Integração final das telas** do sistema.
- **Correções**: bloqueio de hábitos com respostas incorretas.
- **Melhorias**: nova visualização e botão de salvar certificado.
- **Extras**: criação da tela de estatísticas e da página de apresentação.

---

## 🤝 Contribuições

Este projeto foi desenvolvido por estudantes com o propósito de promover a sustentabilidade por meio da tecnologia. Feedbacks e sugestões são sempre bem-vindos!

---

## 📄 Licença

Este projeto é de uso educacional e sem fins lucrativos.



// src/main/java/com/saymyname/service/quiz/QuizPluginConfig.java
package com.saymyname.service.quiz;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.saymyname.service.quiz.plugins.AssociationPlugin;
import com.saymyname.service.quiz.plugins.BinarySwipePlugin;
import com.saymyname.service.quiz.plugins.ClozePlugin;
import com.saymyname.service.quiz.plugins.HangmanPlugin;
import com.saymyname.service.quiz.plugins.McqPlugin;
import com.saymyname.service.quiz.plugins.OrderingPlugin;
import com.saymyname.service.quiz.plugins.TapChoicePlugin;
import com.saymyname.service.quiz.plugins.TextInputPlugin;

@Configuration
public class QuizPluginConfig {

    @Bean
    public TextInputPlugin textInputPlugin() {
        return new TextInputPlugin();
    }

    @Bean
    public ClozePlugin clozePlugin(AnswerKeyService aks) {
        return new ClozePlugin(aks);
    }

    @Bean
    public HangmanPlugin hangmanPlugin(AnswerKeyService aks) {
        return new HangmanPlugin(aks);
    }

    @Bean
    public McqPlugin mcqPlugin(AnswerKeyService aks) {
        return new McqPlugin(aks);
    }

    @Bean
    public TapChoicePlugin tapChoicePlugin(AnswerKeyService aks) {
        return new TapChoicePlugin(aks);
    }

    @Bean
    public BinarySwipePlugin binarySwipePlugin(AnswerKeyService aks) {
        return new BinarySwipePlugin(aks);
    }

    @Bean
    public AssociationPlugin associationPlugin() {
        return new AssociationPlugin();
    }

    @Bean
    public OrderingPlugin orderingPlugin() {
        return new OrderingPlugin();
    }
}

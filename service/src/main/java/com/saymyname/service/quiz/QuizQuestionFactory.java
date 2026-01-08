// src/main/java/com/saymyname/service/quiz/QuizQuestionFactory.java
package com.saymyname.service.quiz;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizPreferredFormat;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizQuestionSpec;
import com.saymyname.service.quiz.plugins.QuizQuestionPlugin;

@Component
public class QuizQuestionFactory {

        private final Map<QuizFormat, QuizQuestionPlugin> plugins;

        public QuizQuestionFactory(List<QuizQuestionPlugin> pluginList) {
                EnumMap<QuizFormat, QuizQuestionPlugin> map = new EnumMap<>(QuizFormat.class);
                for (QuizQuestionPlugin p : pluginList) {
                        map.put(p.supports(), p);
                }
                this.plugins = Map.copyOf(map);
        }

        public QuizQuestion build(QuizQuestionSpec spec, QuizPreferredFormat preferred) {
                QuizFormat fmt = resolve(preferred);
                return plugin(fmt).build(spec);
        }

        public QuizQuestion build(QuizQuestionSpec spec, QuizFormat format) {
                if (format == null) {
                        throw new IllegalArgumentException("format is required");
                }
                return plugin(format).build(spec);
        }

        public QuizQuestionPlugin pluginFor(QuizFormat fmt) {
                return plugin(fmt);
        }

        private QuizFormat resolve(QuizPreferredFormat preferred) {
                if (preferred == null || preferred == QuizPreferredFormat.AUTO) {
                        return QuizFormat.TEXT_INPUT;
                }
                return QuizFormat.valueOf(preferred.name());
        }

        private QuizQuestionPlugin plugin(QuizFormat fmt) {
                QuizQuestionPlugin p = plugins.get(fmt);
                if (p == null) {
                        throw new IllegalStateException("No plugin for format=" + fmt);
                }
                return p;
        }
}

package com.lucassimao.fluxodecaixa.converter;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.TextNode;

import org.javamoney.moneta.Money;
import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class MoneyJsonDeserializer extends JsonDeserializer<Money> {

        @Override
        public Money deserialize(JsonParser jsonParser, DeserializationContext ctxt)
                        throws IOException, JsonProcessingException {

                TreeNode treeNode = jsonParser.getCodec().readTree(jsonParser);
                TextNode currency = (TextNode) treeNode.get("currency");
                NumericNode value = (NumericNode) treeNode.get("value");
                return Money.of(value.decimalValue(), currency.textValue());

        }

}
package com.example.nfe.infrastructure.xml.signature;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.ArrayList;
import java.util.List;

/** XMLDSig {@code ds:Transforms} element — ordered transform list. */
@XmlAccessorType(XmlAccessType.FIELD)
public class Transforms {

    @XmlElement(name = "Transform")
    private List<Transform> transforms = new ArrayList<>();

    public List<Transform> getTransforms() {
        return transforms;
    }

    public void setTransforms(List<Transform> transforms) {
        this.transforms = transforms;
    }
}

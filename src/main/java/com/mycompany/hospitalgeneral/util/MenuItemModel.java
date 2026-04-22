/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.hospitalgeneral.util;

/**
 *
 * @author jhonatan
 */
public class MenuItemModel {

    private String label;
    private String icon;
    private String outcome;
    private String tooltip;

    public MenuItemModel(String label, String icon, String outcome, String tooltip) {
        this.label = label;
        this.icon = icon;
        this.outcome = outcome;
        this.tooltip = tooltip;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

   
}

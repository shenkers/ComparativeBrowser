/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;


public class AlignmentFileLoader implements AlignmentLoader {
    Logger logger = LogManager.getLogger();
    @Override
    public AlignmentSource load(AlignmentType type, String uri) {
        logger.info("returning dummy alignment source");
        return new AlignmentSource(){

            @Override
            public String[] getAlignment(GenomeSpan span1, GenomeSpan span2) {
                logger.info("returning empty alignment");
                return new String[]{"",""};
            }
        };
    }
    
}

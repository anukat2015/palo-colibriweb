	function resizeGraph() {
    	var height = ($(window).height()-80)+'px';
    	var width = ($(window).width()-30)+'px';
    	$("#flowGraphSelectionPanelContent").css('height',height);
    	$("#flowGraphSelectionPanelContent").css('width',width);
	}
	function handleMessage(message) {
    	var textareaElement = document.getElementById("mainform:logPanel:tabs:logDisplay");
    	if (textareaElement!=null) {
			var textarea = $(textareaElement);
			textarea.val(textarea.val()+message); 
			textareaElement.scrollTop = textareaElement.scrollHeight;
    	}
	} 
	function scrollLogs() {
		var textareaElement = document.getElementById("mainform:logPanel:tabs:logDisplay");
    	if (textareaElement!=null) {
			textareaElement.scrollTop = textareaElement.scrollHeight;
    	}
	}
	function callXML() {
    	var height ='500px';
    	$("#mainform\\:xmlEditPanelContent").find(".CodeMirror" ).css('height',height);
    	if (typeof editor != "undefined") {
    		editor.refresh();
    	}
	}
	function refocus(element) {
    	$(element).find(".focus" ).focus();
	}
	function loadNodes(tree, c) {
		//Taken from primefaces tree.js#expandNode with single modification below
	    if (tree.cfg.dynamic) {
//	        if (tree.cfg.cache && tree.getNodeChildrenContainer(c).children().length > 0) {
//	            tree.showNodeChildren(c);
//	            return
//	        }
	        if (c.data("processing")) {
	            PrimeFaces.debug("Node is already being expanded, ignoring expand event.");
	            return
	        }
	        c.data("processing", true);
	        
	        var options = {
	                source: tree.id,
	                process: tree.id,
	                update: tree.id,
	                formId: tree.cfg.formId,
	                params: [
	                    {name: tree.id + '_expandNode', value: tree.getRowKey(c)}
	                ],
	                onsuccess: function(responseXML, status, xhr) {
	                    PrimeFaces.ajax.Response.handle(responseXML, status, xhr, {
	                            widget: tree,
	                            handle: function(content) {
	                                var nodeChildrenContainer = tree.getNodeChildrenContainer(c);
	                                nodeChildrenContainer.empty(); //This is the modification, which avoids duplication of node content
	                                nodeChildrenContainer.append(content);

	                                tree.showNodeChildren(c);

	                                if(tree.cfg.draggable) {                            
	                                    tree.makeDraggable(nodeChildrenContainer.find('span.ui-treenode-content'));
	                                }

	                                if(tree.cfg.droppable) {
	                                    tree.makeDropPoints(nodeChildrenContainer.find('li.ui-tree-droppoint'));
	                                    tree.makeDropNodes(nodeChildrenContainer.find('span.ui-treenode-droppable'));
	                                }
	                            }
	                        });

	                    return true;
	                },
	                oncomplete: function() {
	                    c.removeData('processing');
	                }
	            };
	        
	        if(tree.hasBehavior('expand')) {
                var expandBehavior = tree.cfg.behaviors['expand'];
                expandBehavior.call(tree, options);
            }
            else {
                PrimeFaces.ajax.Request.handle(options);
            }
	    } else {
	        tree.showNodeChildren(c);
	        tree.fireExpandEvent(c);
	    }
	}
	
	function resizeDataDialog() {
		var maxHeight = $(window).height()-230;
		var maxWidth = $(window).width()-45;
		var height = maxHeight+'px';
    	var width = (maxWidth)+'px';
    	$("#mainform\\:dataDisplay").find(".detailseditpanel").css('width',width);
    	
    	var fullTableHeight = $("#mainform\\:dataDisplay").find(".ui-datatable-scrollable-body").innerHeight();
    	var fullTableWidth = $("#mainform\\:dataDisplay").find(".ui-datatable-scrollable-body").innerWidth();
    	$("#mainform\\:dataDisplay").find(".ui-datatable-scrollable-body").css('margin-right','0px;');
    	if (fullTableHeight > maxHeight) {
    		$("#mainform\\:dataDisplay").find(".ui-datatable-scrollable-body").css('height',height);
    	}
    	$("#mainform\\:dataDisplay").find(".ui-treetable-scrollable-body").css('height',height);
    	$("#mainform\\:dataDisplay").find($("[role='grid']")).css('width',width);
	}
	
	/*
	function toggleFunctionPanel(index) {
		$('#mainform').find('.functionContent:eq('+index+')').toggleClass('functionContentHidden');	
	}
	
	function validationCheck(args) {
		if(args.validationFailed) {
			//find all none open function fieldset panels with validation errors and open them 
			
			$('#mainform').find('.invalid').closest('.functionFieldSet').find('.ui-icon-plusthick').each(function() { 
				var index = this.parentNode.parentNode.id.split(':');
				var number = index[2];
				PF('fieldSet'+number).toggle();
			})
			
			$('#mainform').find('.invalid').closest('.functionContent').removeClass('functionContentHidden');
		}
	}
	*/

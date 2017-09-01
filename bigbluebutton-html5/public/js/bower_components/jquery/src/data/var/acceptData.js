define(() =>
/**
 * Determines whether an object can have data
 */
   function (owner) {
	// Accepts only:
	//  - Node
	//    - Node.ELEMENT_NODE
	//    - Node.DOCUMENT_NODE
	//  - Object
	//    - Any
	/* jshint -W018 */
     return owner.nodeType === 1 || owner.nodeType === 9 || !(+owner.nodeType);
   });

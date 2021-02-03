# heirLDA
Heirarchical LDA

Description - Heiracrchical Latent Dirichlet Allocation 
Finding optimal values for symetric Dirichlet hyperparameters for the standard LDA model

Options:
	-alpha (float)
		 initial value of symmetric Dirichlet parameter for document-topic distribution, default=1.0
	-alphaSD (float)
		 standard deviation for alpha proposal distribution, default=0.1
	-beta (float)
		 initial value of symmetric Dirichlet parameter for topic-word distribution, default=20000.0
	-betaSD (float)
		 standard deviation for beta proposal distribution, default=100.0
	-bow input bag of words file path, no default
	-iterations (integer) number of iterations, default=10000
	-out output file path, no default
	-threads (integer) number of threads for multiprocessing, default=1
	-topics (integer) the number of topics, default=50
